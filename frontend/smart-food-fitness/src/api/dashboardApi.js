import apiClient from "./apiClient";

export async function getDashboardSummary(userId, timezone) {
  const res = await apiClient.get(`/api/dashboard-summary/user/${userId}`, {
    params: timezone ? { timezone } : undefined,
  });
  return res.data;
}
