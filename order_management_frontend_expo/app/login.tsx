import { View, Text, Button } from "react-native";
import { useNavigation } from "expo-router";

export default function LoginScreen() {
  const navigation = useNavigation();

  return (
    <View>
      <Text>Login Screen</Text>
      <Button title="Go Back" onPress={() => navigation.goBack()} />
    </View>
  );
}
