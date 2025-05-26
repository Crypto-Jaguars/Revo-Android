from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.response import Response
from .models import Product
from .serializers import ProductSerializer

@api_view(['GET'])
@permission_classes([IsAuthenticated])
def get_inventory(request):
    products = Product.objects.filter(farmer=request.user)
    serializer = ProductSerializer(products, many=True)
    return Response(serializer.data)

@api_view(['POST'])
@permission_classes([IsAuthenticated])
def add_inventory_item(request):
    user = request.user
    data = request.data
    try:
        product = Product.objects.create(
            farmer=user,
            name=data['name'],
            quantity=data['quantity'],
            price=data['price']
        )
        return Response(ProductSerializer(product).data, status=201)
    except Exception as e:
        print("Error creating product:", e)
        return Response({"error": str(e)}, status=500)

@api_view(['GET'])
@permission_classes([AllowAny])
def get_all_inventory(request):
    products = Product.objects.all()
    serializer = ProductSerializer(products, many=True)
    return Response(serializer.data)
