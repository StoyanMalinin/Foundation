import { jwtDecode, JwtPayload } from 'jwt-decode';

class _JWTManager {
    private tokenRaw: string | null
    private tokenDecoded: JwtPayload | null
    constructor() {
        this.tokenRaw = null;
        this.tokenDecoded = null;
    }

    async getTokenRaw(): Promise<string> {
        this.ensureToken();
        if (this.tokenRaw == null) return "";

        return this.tokenRaw ?? "";
    }
    async getUsername(): Promise<String> {
        this.ensureToken();
        if (this.tokenDecoded == null) return "";

        return this.tokenDecoded.sub ?? "";
    }

    private async ensureToken() {
        if (this.tokenDecoded == null || 
            (this.tokenDecoded.exp ?? 0) * 1000 < Date.now()
        ) {
            this.forceRefresh();
        }
    }
    private async forceRefresh() {
        debugger;
        console.log("kade sme be");
        try {
            const res = await fetch('https://localhost:6969/refresh-jwt', {method: "post"});
            const json = await res.json();

            if (res.status != 204) {
                this.tokenRaw = json["token"];
                this.tokenDecoded = jwtDecode(this.tokenRaw ?? ""); 
            }
        } catch(e) {
            console.log(e);
        }
    }
}

export const JWTManager = new _JWTManager();