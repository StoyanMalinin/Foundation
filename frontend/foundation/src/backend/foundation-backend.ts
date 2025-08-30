const BACKEND_API_BASE_URL = "https://ffoundationn.fun:6969";

class _FoundationBackend {
    login(username: string, password: string) {
        return fetch(`${BACKEND_API_BASE_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }),
            credentials: 'include',
        });
    }

    register(data: { username: string, password: string, first_name: string, last_name: string }) {
        return fetch(`${BACKEND_API_BASE_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
            credentials: 'include',
        });
    }

    createSearch(newSearch: { title: string, description: string }) {
        return fetch(`${BACKEND_API_BASE_URL}/create-search`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newSearch),
            credentials: 'include',
        });
    }

    updateSearch(updatedSearch: { id: number, title: string, description: string }) {
        return fetch(`${BACKEND_API_BASE_URL}/update-search`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updatedSearch),
            credentials: 'include',
        });
    }

    getSearch(searchId: number) {
        return fetch(`${BACKEND_API_BASE_URL}/search?id=${searchId}`);
    }

    checkAuth(cookie: string) {
        return fetch(`${BACKEND_API_BASE_URL}/check-auth`, {
            method: 'GET',
            credentials: 'include',
            headers: { 'Cookie': cookie },
        });
    }

    whoAmI() {
        return fetch(`${BACKEND_API_BASE_URL}/who-am-i`, {
            method: 'GET',
            credentials: 'include',
        });
    }

    fetchAdminSearches(jwt: string) {
        return fetch(`${BACKEND_API_BASE_URL}/admin-searches-metadata`, {
            credentials: 'include',
            headers: { 'Authorization': `Bearer ${jwt}` },
        });
    }

    fetchSearches() {
        return fetch(`${BACKEND_API_BASE_URL}/searches-metadata`);
    }

    deleteSearch(searchId: number, jwt: string) {
        return fetch(`${BACKEND_API_BASE_URL}/delete-search?id=${searchId}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: { 'Authorization': `Bearer ${jwt}` },
        });
    }

    getMapTile(searchId: number, z: number, x: number, y: number) {
        return fetch(`${BACKEND_API_BASE_URL}/map-tile?searchId=${searchId}&z=${z}&x=${x}&y=${y}`);
    }
}

export const FoundationBackend = new _FoundationBackend();
