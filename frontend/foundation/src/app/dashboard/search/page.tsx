import AuthWrapper from "@/app/auth/wrapper";
import DashboardSearch from "./dashboard-search";

export default async function Page() {
  return (
    <AuthWrapper>
      <DashboardSearch name="User" />
    </AuthWrapper>
  );
}