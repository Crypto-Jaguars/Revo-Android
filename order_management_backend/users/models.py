from django.db import models
from django.contrib.auth.models import User

class UserProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    full_name = models.CharField(max_length=255)
    nationality = models.CharField(max_length=100)
    role = models.CharField(max_length=50)

    def __str__(self):
        return self.full_name
