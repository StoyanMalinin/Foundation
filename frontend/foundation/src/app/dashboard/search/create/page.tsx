import AuthWrapper from "@/app/auth/wrapper";
import CreateSearch from "./create-search";

export default async function Page() {
    return (
        <AuthWrapper>
            <CreateSearch />
        </AuthWrapper>
    );
}
