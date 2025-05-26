from django.urls import path
from . import views

urlpatterns = [
    path('inventory/', views.get_inventory, name='get-inventory'),  # Farmer's own products
    path('inventory/add/', views.add_inventory_item, name='add-inventory'),  # Add new product
    path('inventory/all/', views.get_all_inventory, name='get-all-inventory'),  # View all products (for buyers)
]
