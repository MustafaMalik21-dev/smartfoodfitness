import apiClient from "./apiClient";

// Function to fetch the dashboard summary for a specific user, accepting the user ID and an optional timezone parameter, making a GET request to the backend API endpoint for retrieving the dashboard summary data, passing the timezone as a query parameter if provided, and returning the response data containing the dashboard summary information to be displayed in the user's dashboard when they access that section of the application
export async function getDashboardSummary(userId, timezone) {
  const res = await apiClient.get(`/api/dashboard-summary/user/${userId}`, {
    params: timezone ? { timezone } : undefined,
  });
  return res.data;
}
