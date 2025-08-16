import AuthWrapper from "@/app/auth/wrapper";
import AdminSearchesMetadataListServer from "./admin-searches-metadata-list-server";

export default async function Page() {
    return <AuthWrapper>
        <AdminSearchesMetadataListServer />
    </AuthWrapper>;
}