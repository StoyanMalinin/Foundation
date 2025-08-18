import AuthWrapper from "@/app/auth/wrapper";
import SearchesMetadataListServer from "@/app/dashboard/search/list/searches-metadata-list";

export default async function Page() {
    return <AuthWrapper>
        <SearchesMetadataListServer></SearchesMetadataListServer>
    </AuthWrapper>
}