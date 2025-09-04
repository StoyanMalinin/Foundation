import AsyncStorage from '@react-native-async-storage/async-storage';

async function getState() : Promise<Record<number, boolean>> {
    const state = await AsyncStorage.getItem("selected_searches");
    return state ? JSON.parse(state) : {};
}

export async function getIsSelected(searchId: number): Promise<boolean> {
  const state = await getState();
  return state[searchId] ?? false;
}

export async function setIsSelected(searchId: number, isSelected: boolean) {
    let state = await getState();
    state[searchId] = isSelected;

    await AsyncStorage.setItem("selected_searches", JSON.stringify(state));
}

export async function getSelectedSearches(): Promise<number[]> {
    const state = await getState();
    return Object.keys(state).filter(key => state[Number(key)]).map(Number);
}