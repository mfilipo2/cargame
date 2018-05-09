
export const errorMixin = {
  data: function () {
    return {
      errors: []
    }
  },
  methods: {
    clearErrors() {
      this.errors = [];
    },
    addError(error) {
      this.errors.push(error);
    },
    addErrorFromResponse(response) {
      if (response && response.data && response.data.message) {
        this.addError(response.data.message)
      } else {
        this.addError('Internal error!');
      }
    }
  }
};
