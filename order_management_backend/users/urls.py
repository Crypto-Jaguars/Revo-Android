from django.urls import path
from users.views import SignupView
from users.views import CustomLoginView
from rest_framework_simplejwt.views import TokenRefreshView
from .views import farmer_profile

urlpatterns = [
    path("auth/signup/", SignupView.as_view(), name="signup"),
    path("api/auth/login/", CustomLoginView.as_view(), name="custom_login"),
    path('farmer/profile/', farmer_profile, name='farmer-profile'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
]
