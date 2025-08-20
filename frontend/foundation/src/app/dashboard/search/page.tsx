import DashboardSearch from "./dashboard-search";
import { NavigationBar } from "@/app/navigation/navigation-bar";

export default async function Page() {
  return (
    <>
      <NavigationBar />
      <DashboardSearch/>
    </>
  );
}