/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        heading: ['Lora', 'Georgia', 'serif'],
        body: ['Crimson Pro', 'Georgia', 'serif']
      },
      colors: {
        journal: {
          cream: '#F5F0E8',
          paper: '#FDFBF7',
          ink: '#3D2C29',
          inkMuted: '#6B5B52',
          accent: '#2D5A27',
          accentHover: '#3D7A37',
          line: '#D4CEC4',
          fold: '#E8E2D8'
        },
        journalDark: {
          bg: '#2C2A26',
          paper: '#36342E',
          ink: '#E8E4DC',
          inkMuted: '#A8A49C',
          accent: '#8B9F6A',
          accentHover: '#9FB37A',
          line: '#4A4842',
          fold: '#3E3C36'
        }
      },
      boxShadow: {
        paper: '0 1px 3px rgba(61, 44, 41, 0.08), 2px 2px 0 rgba(61, 44, 41, 0.03)',
        paperFold: '2px 2px 6px rgba(61, 44, 41, 0.12)',
        paperDark: '0 1px 3px rgba(0, 0, 0, 0.3), 2px 2px 0 rgba(0, 0, 0, 0.2)'
      }
    }
  },
  plugins: []
}

