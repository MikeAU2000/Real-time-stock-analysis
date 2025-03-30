import { createRouter, createWebHistory } from 'vue-router'
import StockChartView from '../views/StockChartView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: StockChartView,
    },
  ],
})

export default router
