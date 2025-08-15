import { jwtDecode, JwtPayload } from 'jwt-decode';

class _JWTManager {
    private tokenRaw: string | null
    private tokenDecoded: JwtPayload | null
    constructor() {
        this.tokenRaw = null;
        this.tokenDecoded = null;
    }

    async getTokenRaw(): Promise<string | null> {
        await this.ensureToken();
        return this.tokenRaw;
    }
    async getUsername(): Promise<String | null> {
        await this.ensureToken(); 
        return this.tokenDecoded?.sub ?? null;
    }

    private async ensureToken() {
        if (this.tokenDecoded == null || 
            (this.tokenDecoded.exp ?? 0) * 1000 < Date.now()
        ) {
            await this.forceRefresh();
        }
    }
    private async forceRefresh() {
        try {
            const res = await fetch('https://localhost:6969/refresh-jwt', {
                method: "post",
                credentials: 'include',
            });
            if (res.status == 200) {
                const json = await res.json();

                this.tokenRaw = json["token"];
                this.tokenDecoded = jwtDecode(this.tokenRaw ?? "");
            }
        } catch(e) {
            console.log(e);
        }
    }
}

export const JWTManager = new _JWTManager();