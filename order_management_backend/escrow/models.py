from django.db import models
from django.contrib.auth.models import User

class EscrowTransaction(models.Model):
    STATUS_CHOICES = [
        ('pending', 'Pending'),
        ('funded', 'Funded'),
        ('released', 'Released'),
        ('disputed', 'Disputed'),
        ('cancelled', 'Cancelled'),
    ]

    transaction_id = models.CharField(max_length=100, unique=True)
    buyer = models.ForeignKey(User, on_delete=models.CASCADE, related_name='buyer_transactions')
    seller = models.ForeignKey(User, on_delete=models.CASCADE, related_name='seller_transactions')
    amount = models.DecimalField(max_digits=10, decimal_places=2)
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='pending')
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    blockchain_reference = models.CharField(max_length=255, blank=True, null=True)  # Hash or Transaction ID on Blockchain

    def __str__(self):
        return f"Escrow {self.transaction_id} - {self.status}"
