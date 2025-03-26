import React, { useState } from "react";
import { View, Text, TextInput, Button, FlatList, StyleSheet } from "react-native";

const ChatScreen = () => {
  const [messages, setMessages] = useState([
    { id: "1", sender: "buyer", content: "Hello, is my order ready?" },
    { id: "2", sender: "farmer", content: "Yes! We are preparing it now." },
  ]);
  const [newMessage, setNewMessage] = useState("");

  const sendMessage = () => {
    if (!newMessage.trim()) return;
    setMessages([...messages, { id: Date.now().toString(), sender: "buyer", content: newMessage }]);
    setNewMessage("");
  };

  return (
    <View style={styles.container}>
      <FlatList
        data={messages}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <View style={item.sender === "buyer" ? styles.buyerMessage : styles.farmerMessage}>
            <Text style={styles.messageText}>{item.content}</Text>
          </View>
        )}
      />
      <View style={styles.inputContainer}>
        <TextInput
          style={styles.input}
          placeholder="Type a message..."
          value={newMessage}
          onChangeText={setNewMessage}
        />
        <Button title="Send" onPress={sendMessage} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 10, backgroundColor: "#f5f5f5" },
  buyerMessage: { alignSelf: "flex-end", backgroundColor: "green", padding: 10, borderRadius: 5, marginVertical: 5 },
  farmerMessage: { alignSelf: "flex-start", backgroundColor: "gray", padding: 10, borderRadius: 5, marginVertical: 5 },
  messageText: { color: "white" },
  inputContainer: { flexDirection: "row", alignItems: "center", marginTop: 10 },
  input: { flex: 1, borderWidth: 1, padding: 10, marginRight: 5, borderRadius: 5 },
});

export default ChatScreen;
