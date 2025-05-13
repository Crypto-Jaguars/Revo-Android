from django.urls import path
from . import views

urlpatterns = [
    path('inventory/', views.get_inventory, name='get-inventory'),
    path('inventory/add/', views.add_inventory_item, name='add-inventory'),
]
