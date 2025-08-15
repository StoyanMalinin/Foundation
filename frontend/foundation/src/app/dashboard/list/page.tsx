import AuthWrapper from "@/app/auth/wrapper";
import SearchesMetadataList from "./searches-metadata-list";

export default async function Page() {
    return <AuthWrapper>
        <SearchesMetadataList />
    </AuthWrapper>;
}