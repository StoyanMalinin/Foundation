import { FoundationBackend } from "@/backend/foundation-backend";
import { Button } from "@react-navigation/elements";
import React, { useEffect, useState } from "react";
import { Text, View } from "react-native";
import { AuthWrapper } from "../auth/auth-wrapper";
import { getIsSelected, setIsSelected } from "./storage";

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
    return <View><Text>Loading searches...</Text></View>
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
        {renderSearches(selectedSearches, [searches, setSearches], false)}

        <Text>{"\n\n"}</Text>

        <Text style={{ fontWeight: "bold", fontSize: 26 }}>Searches not selected:</Text>
        {renderSearches(notSelectedSearches, [searches, setSearches], true)}
      </View>
    </AuthWrapper>
  );
}

function renderSearches(searchesToRender, [searches, setSearches], select) {
  if (searchesToRender.length == 0) {
    return <Text>None</Text>;
  }

  return searchesToRender.map(search => (
    <View key={search.id} style={{ flexDirection: "row", alignItems: "center" }}>
      <Text>{search.title}  </Text>
      <Button onPress={async () => {
        await setIsSelected(search.id, select);
        setSearches(searches.map(s => s.id === search.id ? {...s, userSelected: select} : s));
      }}>{select ? "Select" : "Unselect"}</Button>
    </View>
  ));
}
