from django.contrib.auth.models import User
from django.contrib.auth import authenticate, login
from rest_framework.response import Response
from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
from .serializers import UserSerializer
from .serializers import UserProfileSerializer
from django.utils.crypto import get_random_string
from rest_framework.authtoken.models import Token
from users.models import UserProfile 
from rest_framework.decorators import permission_classes
from rest_framework.permissions import IsAuthenticated


class SignupView(APIView):
    def post(self, request):
        email = request.data.get("email")
        password = request.data.get("password")
        full_name = request.data.get("fullName")
        role = request.data.get("role")
        nationality = request.data.get("nationality")

        if not email or not password:
            return Response({"message": "Email and password are required."}, status=400)

        username = email.split("@")[0]

        if User.objects.filter(username=username).exists():
            return Response({"message": "User already exists."}, status=400)

        user = User(username=username, email=email)
        user.set_password(password)
        user.save()

        # Optionally handle extra fields here if you're using a profile or custom model

        return Response({"success": True, "user": UserSerializer(user).data}, status=201)

class CustomLoginView(APIView):
    def post(self, request):
        email = request.data.get("email")
        password = request.data.get("password")
        role = request.data.get("role")

        print("Login attempt:", email, role)

        if not all([email, password, role]):
            print("Missing field")
            return Response({'success': False, 'message': 'All fields are required.'}, status=400)

        try:
            user = User.objects.get(email=email)
        except User.DoesNotExist:
            print("User not found")
            return Response({'success': False, 'message': 'User not found.'}, status=400)

        if not user.check_password(password):
            print("Wrong password")
            return Response({'success': False, 'message': 'Incorrect password.'}, status=400)

        try:
            profile = UserProfile.objects.get(user=user)
        except UserProfile.DoesNotExist:
            return Response({'success': False, 'message': 'User profile not found. Please contact support.'}, status=400)

        if profile.role.lower() != role.lower():
            print("Role mismatch")
            return Response({'success': False, 'message': 'Role mismatch.'}, status=400)

        refresh = RefreshToken.for_user(user)
        print("Login success")

        return Response({
            'success': True,
            'token': str(refresh.access_token),
            'refresh': str(refresh),
            'user_role': profile.role
        })

@api_view(['GET', 'PUT'])
@permission_classes([IsAuthenticated])
def farmer_profile(request):
    profile = request.user.userprofile
    if profile.role != "Farmer":
        return Response({"detail": "Access denied: not a farmer."}, status=403)

    if request.method == 'GET':
        serializer = UserProfileSerializer(profile)
        return Response(serializer.data)

    elif request.method == 'PUT':
        serializer = UserProfileSerializer(profile, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=400)
