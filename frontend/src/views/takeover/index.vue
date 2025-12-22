<template>
  <TakeoverList />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import TakeoverList from './components/TakeoverList.vue';

export default defineComponent({
  name: 'TakeoverView',
  components: {
    TakeoverList
  },
  setup() {
    return {};
  }
});
</script>
