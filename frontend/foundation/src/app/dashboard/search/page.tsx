import AuthWrapper from "@/app/auth/wrapper";
import DashboardSearch from "./dashboard-search";
import { NavigationBar } from "@/app/navigation/navigation-bar";

export default async function Page() {
  return (
    <AuthWrapper>
      <NavigationBar />
      <DashboardSearch/>
    </AuthWrapper>
  );
}