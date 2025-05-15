import React, { useEffect, useRef, useState } from "react";
import { View, TextInput, Button, FlatList, Text, StyleSheet } from "react-native";

const ChatDetailScreen = ({ route }) => {
  const { roomName } = route.params;
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([]);
  const ws = useRef(null);

  useEffect(() => {
    ws.current = new WebSocket(`ws://<your-ip>:8000/ws/chat/${roomName}/`);

    ws.current.onmessage = (e) => {
      const data = JSON.parse(e.data);
      setMessages((prev) => [...prev, { sender: "other", text: data.message }]);
    };

    ws.current.onerror = (e) => console.error("WebSocket error:", e.message);
    ws.current.onclose = () => console.log("WebSocket closed");

    return () => ws.current?.close();
  }, [roomName]);

  const sendMessage = () => {
    if (message.trim() && ws.current?.readyState === WebSocket.OPEN) {
      ws.current.send(JSON.stringify({ message }));
      setMessages((prev) => [...prev, { sender: "me", text: message }]);
      setMessage("");
    }
  };

  return (
    <View style={styles.container}>
      <FlatList
        data={messages}
        keyExtractor={(_, index) => index.toString()}
        renderItem={({ item }) => (
          <Text style={item.sender === "me" ? styles.myMessage : styles.otherMessage}>
            {item.text}
          </Text>
        )}
      />
      <View style={styles.inputRow}>
        <TextInput
          style={styles.input}
          value={message}
          onChangeText={setMessage}
          placeholder="Type your message..."
        />
        <Button title="Send" onPress={sendMessage} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16 },
  inputRow: { flexDirection: "row", alignItems: "center", marginTop: 8 },
  input: { flex: 1, borderWidth: 1, borderColor: "#ccc", borderRadius: 4, padding: 8, marginRight: 8 },
  myMessage: { alignSelf: "flex-end", backgroundColor: "#dcf8c6", padding: 10, marginVertical: 4, borderRadius: 8 },
  otherMessage: { alignSelf: "flex-start", backgroundColor: "#f1f0f0", padding: 10, marginVertical: 4, borderRadius: 8 },
});

export default ChatDetailScreen;
