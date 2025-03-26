from django.contrib import admin
from django.urls import path, include
from users.views import SignupView, LoginView

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/auth/', include('users.urls')), 
    path("api/auth/signup/", SignupView.as_view(), name="signup"),
    path('api/auth/login/', LoginView.as_view(), name='login'),
]
