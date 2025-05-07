from django.db.models.signals import post_save
from django.dispatch import receiver
from django.contrib.auth.models import User
from .models import UserProfile

@receiver(post_save, sender=User)
def create_user_profile(sender, instance, created, **kwargs):
    if created and not hasattr(instance, 'userprofile'):
        UserProfile.objects.create(
            user=instance,
            full_name=instance.username,
            nationality='Unknown',
            role='Buyer'  # Or set dynamically during signup
        )
