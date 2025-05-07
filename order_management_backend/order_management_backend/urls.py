from django.contrib import admin
from django.urls import path, include
from users.views import SignupView
from users.views import CustomLoginView

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/auth/', include('users.urls')), 
    path("api/auth/signup/", SignupView.as_view(), name="signup"),
    path("api/auth/login/", CustomLoginView.as_view(), name="custom_login"),
]
