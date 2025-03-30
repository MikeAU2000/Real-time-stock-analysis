<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
/* 全局样式 */
body {
  margin: 0;
  padding: 0;
  font-family: Arial, sans-serif;
  background-color: #000;
  color: white;
  overflow: hidden; /* 防止滚动条出现 */
}

html,
body,
#app {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>
