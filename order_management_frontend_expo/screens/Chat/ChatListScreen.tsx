import React from "react";
import { View, Text, FlatList, TouchableOpacity, StyleSheet } from "react-native";

const mockChats = [
  { id: "1", name: "Buyer 1" },
  { id: "2", name: "Buyer 2" },
];

const ChatListScreen = ({ navigation }) => {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Chat with Buyers</Text>
      <FlatList
        data={mockChats}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <TouchableOpacity
            style={styles.chatItem}
            onPress={() => navigation.navigate("ChatDetail", { roomName: item.name })}
          >
            <Text style={styles.chatName}>{item.name}</Text>
          </TouchableOpacity>
        )}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { padding: 16, flex: 1, backgroundColor: "#fff" },
  title: { fontSize: 24, fontWeight: "bold", marginBottom: 20 },
  chatItem: { padding: 16, borderBottomWidth: 1, borderColor: "#eee" },
  chatName: { fontSize: 18 },
});

export default ChatListScreen;
