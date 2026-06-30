import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(sessionStorage.getItem('loginUser') || 'null'))

  function setUser(u) {
    user.value = u
    sessionStorage.setItem('loginUser', JSON.stringify(u))
  }

  function clearUser() {
    user.value = null
    sessionStorage.removeItem('loginUser')
  }

  function isAdmin() {
    return user.value?.role === 'ADMIN'
  }

  return { user, setUser, clearUser, isAdmin }
})
