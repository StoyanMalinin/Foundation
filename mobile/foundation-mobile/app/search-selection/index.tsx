import { FoundationBackend } from "@/backend/foundation-backend";
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Button } from "@react-navigation/elements";
import React, { useEffect, useState } from "react";
import { Text, View } from "react-native";
import { AuthWrapper } from "../auth/auth-wrapper";

type SearchMetadata = {
  id: number;
  title: string;

  userSelected: boolean;
};

export default function Index() {
  const [searches, setSearches] = useState<SearchMetadata[] | null>([]);
  useEffect(() => {
      const fetchSearches = async () => {
        const response = await FoundationBackend.searchesMetadata();
        const data = await response.json() as SearchMetadata[];

        await Promise.all(data.map(async search => {
          search.userSelected = await getIsSelected(search.id);
        }));
        setSearches(data);
      };

      fetchSearches();
  }, []);

  if (searches == null) {
    return <View><Text>Loading...</Text></View>
  }

  const selectedSearches = searches.filter(search => search.userSelected);
  const notSelectedSearches = searches.filter(search => !search.userSelected);

  return (
    <AuthWrapper>
      <View
        style={{
          flex: 1,
          justifyContent: "center",        
          alignItems: "center",
      }}
      >
        <Text style={{ fontWeight: "bold", fontSize: 26 }}>Selected Searches:</Text>
        {selectedSearches.length == 0 ? <Text>None</Text> : selectedSearches.map(search => (
          <View key={search.id} style={{ flexDirection: "row", alignItems: "center" }}>
            <Text>{search.title}   </Text>
            <Button onPress={() => {
              setIsSelected(search.id, false);
              setSearches(searches.map(s => 
                s.id === search.id ? { ...s, userSelected: false } : s
              ));
            }}>Unselect</Button>
          </View>
        ))}

        <Text>{"\n\n"}</Text>

        <Text style={{ fontWeight: "bold", fontSize: 26 }}>Searches not selected:</Text>
        {notSelectedSearches.length == 0 ? <Text>None</Text> : notSelectedSearches.map(search => (
          <View key={search.id} style={{ flexDirection: "row", alignItems: "center" }}>
            <Text>{search.title}  </Text>
            <Button onPress={() => {
              setIsSelected(search.id, true);
              setSearches(searches.map(s => 
                s.id === search.id ? { ...s, userSelected: true } : s
              ));
            }}>Select</Button>
          </View>
        ))}
      </View>
    </AuthWrapper>
  );
}

async function getIsSelected(searchId: number): Promise<boolean> {
  const value = await AsyncStorage.getItem(`search_${searchId}_selected`);
  if (value === null) {
    return false;
  }

  return value === "true";
}

async function setIsSelected(searchId: number, isSelected: boolean) {
  await AsyncStorage.setItem(`search_${searchId}_selected`, JSON.stringify(isSelected));
}
