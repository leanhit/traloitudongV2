import { ref, watch } from 'vue'

defineProps<{
  visible: boolean
  loading: boolean
}>()

defineEmits(['update:visible', 'submit'])

const name = ref('')
