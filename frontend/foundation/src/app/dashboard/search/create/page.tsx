import AuthWrapper from "@/app/auth/wrapper";
import CreateSearch from "./create-search";
import { NavigationBar } from "@/app/navigation/navigation-bar";

export default async function Page() {
    return (
        <AuthWrapper>
            <NavigationBar />
            <CreateSearch />
        </AuthWrapper>
    );
}
