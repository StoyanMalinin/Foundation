import AuthWrapper from "@/app/auth/wrapper";
import AdminSearchesMetadataListServer from "./admin-searches-metadata-list-server";
import { NavigationBar } from "@/app/navigation/navigation-bar";

export default async function Page() {
    return <AuthWrapper>
        <NavigationBar />
        <AdminSearchesMetadataListServer />
    </AuthWrapper>;
}