import AuthWrapper from "@/app/auth/wrapper";
import SearchesMetadataListServer from "./searches-metadata-list-server";

export default async function Page() {
    return <AuthWrapper>
        <SearchesMetadataListServer />
    </AuthWrapper>;
}