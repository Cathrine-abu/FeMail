import './Button.css';

function Button({ onClick, children, disabled, variant = 'blue'}) {
  const variantClass = {
    blue: 'btn',
    grey: 'btn btn-grey',
  }[variant];

  return (
    <button onClick={onClick} className={variantClass} disabled={disabled}>
      {children}
    </button>
  );
}

export default Button;
