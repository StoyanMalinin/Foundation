const BACKEND_API_BASE_URL_FROM_SERVER = process.env.NEXT_PUBLIC_BACKEND_API_BASE_URL_FROM_SERVER || "";
const BACKEND_API_BASE_URL_FROM_BROWSER = process.env.NEXT_PUBLIC_BACKEND_API_BASE_URL_FROM_BROWSER || "";

function isBrowser() {
  return typeof window !== "undefined";
}

function getBackendAPIURL() {
    if (isBrowser()) {
        return BACKEND_API_BASE_URL_FROM_BROWSER;
    }

    return BACKEND_API_BASE_URL_FROM_SERVER;
}

class _FoundationBackend {
    login(username: string, password: string) {
        return fetch(`${getBackendAPIURL()}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }),
            credentials: 'include',
        });
    }

    register(data: { username: string, password: string, first_name: string, last_name: string }) {
        return fetch(`${getBackendAPIURL()}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
            credentials: 'include',
        });
    }

    createSearch(newSearch: { title: string, description: string }) {
        return fetch(`${getBackendAPIURL()}/create-search`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newSearch),
            credentials: 'include',
        });
    }

    updateSearch(updatedSearch: { id: number, title: string, description: string }) {
        return fetch(`${getBackendAPIURL()}/update-search`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updatedSearch),
            credentials: 'include',
        });
    }

    getSearch(searchId: number) {
        return fetch(`${getBackendAPIURL()}/search?id=${searchId}`);
    }

    checkAuth(cookie: string) {
        return fetch(`${getBackendAPIURL()}/check-auth`, {
            method: 'GET',
            credentials: 'include',
            headers: { 'Cookie': cookie },
        });
    }

    whoAmI() {
        return fetch(`${getBackendAPIURL()}/who-am-i`, {
            method: 'GET',
            credentials: 'include',
        });
    }

    fetchAdminSearches(jwt: string) {
        return fetch(`${getBackendAPIURL()}/admin-searches-metadata`, {
            credentials: 'include',
            headers: { 'Authorization': `Bearer ${jwt}` },
        });
    }

    fetchSearches() {
        return fetch(`${getBackendAPIURL()}/searches-metadata`);
    }

    deleteSearch(searchId: number, jwt: string) {
        return fetch(`${getBackendAPIURL()}/delete-search?id=${searchId}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: { 'Authorization': `Bearer ${jwt}` },
        });
    }

    getMapTile(searchId: number, z: number, x: number, y: number) {
        return fetch(`${getBackendAPIURL()}/map-tile?searchId=${searchId}&z=${z}&x=${x}&y=${y}`);
    }
}

export const FoundationBackend = new _FoundationBackend();
