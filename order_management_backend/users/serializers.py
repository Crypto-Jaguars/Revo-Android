from rest_framework import serializers
from django.contrib.auth.models import User

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'password']
        extra_kwargs = {'password': {'write_only': True}}

    def create(self, validated_data):
        email = validated_data.get('email')
        password = validated_data.pop('password', None)  
        username = validated_data.get('username', email.split("@")[0]) 

        user = User(username=username, email=email, **validated_data)
        if password:
            user.set_password(password) 
        user.save()
        return user
