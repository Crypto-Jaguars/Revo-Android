from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from .models import FarmerProfile
from .serializers import FarmerProfileSerializer

@api_view(['GET'])
@permission_classes([IsAuthenticated])
def farmer_dashboard(request):
    try:
        profile = FarmerProfile.objects.get(user=request.user)
        serializer = FarmerProfileSerializer(profile)
        return Response(serializer.data)
    except FarmerProfile.DoesNotExist:
        return Response({'detail': 'Farmer profile not found.'}, status=404)
