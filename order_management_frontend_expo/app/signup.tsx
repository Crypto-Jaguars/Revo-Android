import { View, Text, Button } from "react-native";
import { useNavigation } from "expo-router";

export default function SignupScreen() {
  const navigation = useNavigation();

  return (
    <View>
      <Text>Signup Screen</Text>
      <Button title="Go Back" onPress={() => navigation.goBack()} />
    </View>
  );
}
