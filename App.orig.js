import 'expo-dev-client';
import React, { useCallback, useState } from 'react';
import {
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';

export default function App() {
  const [form, setForm] = useState({ username: '', password: '' });
  const [loading, setLoading] = useState(false);
  const handleChange = useCallback(
    (fieldName) => (value) =>
      setForm((form) => ({ ...form, [fieldName]: value })),
    [],
  );

  const handleSubmit = useCallback(() => {
    console.log('Submitting form: ', form);
    setLoading(true);
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        // Simulate success
        resolve('Simulated data from the server');

        // Simulate an error
        if (Math.random() * 10 < 1) reject(new Error('Simulated error'));
        setLoading(false);
      }, 1500);
    });
  }, [form]);

  return (
    <View style={styles.container}>
      <TextInput
        placeholder="Username"
        value={form.username}
        style={styles.textInput}
        onChangeText={handleChange('username')}
      />
      <TextInput
        placeholder="Password"
        value={form.password}
        style={styles.textInput}
        onChangeText={handleChange('password')}
        secureTextEntry
      />
      <TouchableOpacity
        style={loading ? styles.buttonDisabled : styles.button}
        onPress={() => handleSubmit(form)}
        disabled={loading}
      >
        <Text>Login</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  textInput: {
    height: 40,
    borderColor: 'gray',
    borderWidth: 1,
    borderRadius: 4,
    width: '100%',
    paddingHorizontal: 10,
    marginBottom: 10,
  },
  button: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 12,
    paddingHorizontal: 32,
    borderRadius: 4,
    elevation: 3,
    backgroundColor: '#2196F3',
    width: '100%',
    marginBottom: 10,
  },
  buttonDisabled: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 12,
    paddingHorizontal: 32,
    borderRadius: 4,
    elevation: 3,
    backgroundColor: '#ccc',
    width: '100%',
    marginBottom: 10,
  },
});
