import { createStore } from 'vuex'

export default createStore({
  state: {
    user: null
  },
  mutations: {
    setUser(state, user) {
      state.user = user
    },
    clearUser(state) {
      state.user = null
    },
    initializeUser(state) {
      const token = localStorage.getItem('token')
      const userStr = localStorage.getItem('user')
      
      if (token && userStr) {
        try {
          const user = JSON.parse(userStr)
          state.user = user
        } catch (error) {
          console.error('解析用户信息失败:', error)
          localStorage.removeItem('token')
          localStorage.removeItem('user')
        }
      }
    }
  },
  actions: {
    login({ commit }, user) {
      commit('setUser', user)
    },
    logout({ commit }) {
      commit('clearUser')
    },
    initializeApp({ commit }) {
      commit('initializeUser')
    }
  },
  getters: {
    isLoggedIn: state => !!state.user,
    currentUser: state => state.user
  }
}) 