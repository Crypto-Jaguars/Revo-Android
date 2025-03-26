import React from "react";
import { View, Text, Button, StyleSheet } from "react-native";
import { useNavigation } from "@react-navigation/native";

const EscrowReleaseScreen = () => {
  const navigation = useNavigation();

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Escrow Payment Release</Text>
      <Text style={styles.description}>
        Verify the order completion before releasing the escrow payment.
      </Text>

      <Button title="Release Payment" color="green" onPress={() => alert("Payment Released!")} />
      <Button title="Back to Dashboard" color="gray" onPress={() => navigation.goBack()} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: "center", alignItems: "center", padding: 20 },
  title: { fontSize: 24, fontWeight: "bold", marginBottom: 10 },
  description: { fontSize: 16, textAlign: "center", marginBottom: 20 },
});

export default EscrowReleaseScreen;
