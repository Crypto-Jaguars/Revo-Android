from django.urls import path
from . import views

urlpatterns = [
    path('dashboard/', views.farmer_dashboard, name='farmer-dashboard'),
]
