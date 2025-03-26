import React from "react";
import { View, Text, StyleSheet } from "react-native";

const ViewOrdersScreen = () => {
  return (
    <View style={styles.container}>
      <Text style={styles.header}>Order Tracking</Text>
      <Text>Status: Shipped 🚚</Text>
      <Text>Estimated Delivery: 2 days</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20 },
  header: { fontSize: 22, fontWeight: "bold", marginBottom: 10 }
});

export default ViewOrdersScreen;
