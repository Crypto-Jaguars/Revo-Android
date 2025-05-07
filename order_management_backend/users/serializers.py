from django.contrib.auth.models import User
from rest_framework import serializers
from users.models import UserProfile

class UserSerializer(serializers.ModelSerializer):
    full_name = serializers.CharField(write_only=True)
    nationality = serializers.CharField(write_only=True)
    role = serializers.CharField(write_only=True)

    class Meta:
        model = User
        fields = ['id', 'email', 'username', 'password', 'full_name', 'nationality', 'role']
        extra_kwargs = {'password': {'write_only': True}}

class UserProfileSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserProfile
        fields = ['full_name', 'nationality', 'role'] 

    def create(self, validated_data):
        email = validated_data['email']
        password = validated_data['password']
        full_name = validated_data.pop('full_name')
        nationality = validated_data.pop('nationality')
        role = validated_data.pop('role')

        base_username = email.split("@")[0]
        username = base_username
        counter = 1

        while User.objects.filter(username=username).exists():
            username = f"{base_username}{counter}"
            counter += 1

        user = User(username=username, email=email)
        user.set_password(password)
        user.save()

        UserProfile.objects.create(
            user=user,
            full_name=full_name,
            nationality=nationality,
            role=role
        )

        return user
