import React, { useState, useEffect } from "react";
import { View, Text, FlatList, TouchableOpacity, StyleSheet } from "react-native";
import { useNavigation } from "@react-navigation/native";

const orders = [
  { id: "1", status: "Pending", total: "$50", date: "2025-03-21" },
  { id: "2", status: "Confirmed", total: "$30", date: "2025-03-20" },
  { id: "3", status: "Shipped", total: "$120", date: "2025-03-18" }
];

const OrderListScreen = () => {
  const navigation = useNavigation();

  return (
    <View style={styles.container}>
      <Text style={styles.header}>My Orders</Text>
      <FlatList
        data={orders}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <TouchableOpacity
            style={styles.orderItem}
            onPress={() => navigation.navigate("OrdersDetailScreen", { orderId: item.id  })}
            >
            <Text>Order #{item.id}</Text>
            <Text>Status: {item.status}</Text>
            <Text>Total: {item.total}</Text>
            <Text>Date: {item.date}</Text>
          </TouchableOpacity>
        )}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20 },
  header: { fontSize: 20, fontWeight: "bold", marginBottom: 10 },
  orderItem: {
    padding: 15,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: "#ddd",
    borderRadius: 5
  }
});

export default OrderListScreen;
