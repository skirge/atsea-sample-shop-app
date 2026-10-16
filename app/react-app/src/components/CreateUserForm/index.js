import React, { PropTypes, Component } from 'react';
import { Field, reduxForm } from 'redux-form';
import { FlatButton } from 'material-ui';
import Input from '../Input';
import './styles.css';
import validate from './validate.js';

const renderUsername = field => <Input field={field} hintText={"Choose a user ID"} />;
const renderPassword = field => <Input type={"password"} field={field} hintText={"Choose a password"} />;

class CreateUserForm extends Component {

  renderCreateUser() {
    const header = 'Create your user ID'
    return (
      <div>
        <div className='createFormHeader'>
          {header}
        </div>
        <div className='createFormRow'>
          <Field
            name="username"
            component={renderUsername}
          />
          <Field
            name="password"
            component={renderPassword}
          />
        </div>
      </div>
    );
  }

  renderButtons() {
    const { handleSubmit } = this.props
    const labelStyles = {
      textTransform: 'none',
      fontFamily: 'Open Sans',
      fontWeight: 600,
    };
    const styles = {
      color: '#fff',
      backgroundColor: '#099CEC',
    };

    return (
      <div className='createFormButton'>
        <FlatButton
          label="Sign up"
          onClick={handleSubmit}
          style={styles}
          labelStyle={labelStyles}
        />
      </div>
    );
  }

  render() {
    const {
      handleSubmit,
    } = this.props;

    return (
      <div className='createFormContent'>
        <form onSubmit={handleSubmit}>
          {this.renderCreateUser()}
          {this.renderButtons()}
        </form>
      </div>
    );
  }
}

CreateUserForm.propTypes = {
  handleSubmit: PropTypes.func.isRequired,
};

export default CreateUserForm = reduxForm({
  form: 'createUserForm',
  validate,
})(CreateUserForm);
