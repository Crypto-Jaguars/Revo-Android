from django.contrib import admin
from .models import Product, InventoryTransaction

admin.site.register(Product)
admin.site.register(InventoryTransaction)
