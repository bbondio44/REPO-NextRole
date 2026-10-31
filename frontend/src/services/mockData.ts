import type {
  LearningResource,
  Role,
  RoleSkillRequirement,
  Skill,
  SkillCategory,
} from '@/types/domain'

export const skillCategories: SkillCategory[] = [
  { id: 'programming', name: 'Programación', icon: 'code' },
  { id: 'frontend', name: 'Frontend', icon: 'layout' },
  { id: 'backend', name: 'Backend', icon: 'server' },
  { id: 'data', name: 'Datos', icon: 'database' },
  { id: 'infra', name: 'Infra & Cloud', icon: 'cloud' },
  { id: 'testing', name: 'Testing', icon: 'check' },
  { id: 'security', name: 'Seguridad', icon: 'shield' },
  { id: 'tools', name: 'Herramientas', icon: 'tool' },
  { id: 'soft', name: 'Soft Skills', icon: 'people' },
]

export const skills: Skill[] = [
  { id: 'html-css', categoryId: 'frontend', name: 'HTML5 & CSS3 semántico', description: 'Maquetado semántico y responsive.' },
  { id: 'js-ts', categoryId: 'programming', name: 'JavaScript / TypeScript', description: 'Frontend & Backend.' },
  { id: 'react', categoryId: 'frontend', name: 'React.js básico / components', description: 'Componentes, estado y props.' },
  { id: 'nextjs', categoryId: 'frontend', name: 'Next.js & SSR', description: 'App Router y renderizado en servidor.' },
  { id: 'nodejs', categoryId: 'backend', name: 'Node.js', description: 'Entornos de ejecución y servicios backend.' },
  { id: 'sql', categoryId: 'data', name: 'Bases de datos SQL', description: 'PostgreSQL / MySQL.' },
  { id: 'rest-apis', categoryId: 'backend', name: 'APIs REST & Webhooks', description: 'Consumo y diseño.' },
  { id: 'git', categoryId: 'tools', name: 'Git y Control de Versiones', description: 'GitHub / GitLab Flow.' },
  { id: 'testing', categoryId: 'testing', name: 'Testing y QA', description: 'Pruebas unitarias y end-to-end.' },
  { id: 'linux', categoryId: 'infra', name: 'Linux y línea de comandos', description: 'Administración básica de sistemas.' },
  { id: 'cloud', categoryId: 'infra', name: 'Cloud básico (AWS/Azure)', description: 'Servicios de cómputo y despliegue.' },
  { id: 'security', categoryId: 'security', name: 'Seguridad básica (OWASP)', description: 'Buenas prácticas de seguridad web.' },
  { id: 'python', categoryId: 'programming', name: 'Python', description: 'Scripting y análisis de datos.' },
  { id: 'teamwork', categoryId: 'soft', name: 'Trabajo en equipo', description: 'Scrum / Feedback.' },
  { id: 'communication', categoryId: 'soft', name: 'Comunicación y resolución de problemas', description: 'Claridad al reportar y colaborar.' },
]

export const roles: Role[] = [
  {
    id: 'frontend-dev-jr',
    name: 'Frontend Developer Trainee / Jr',
    area: 'Desarrollo',
    icon: 'code',
    description: 'Diseña y construye interfaces web modernas, responsivas e interactivas integrando APIs.',
    responsibilities: [
      'Implementar interfaces a partir de diseños en Figma.',
      'Consumir APIs REST y manejar estados de carga y error.',
      'Garantizar accesibilidad y diseño responsive.',
    ],
    demandTag: 'Alta demanda laboral',
  },
  {
    id: 'backend-dev-jr',
    name: 'Backend Developer Jr',
    area: 'Desarrollo',
    icon: 'server',
    description: 'Implementa la lógica de negocio, la persistencia y las APIs que consume el frontend.',
    responsibilities: [
      'Diseñar e implementar endpoints REST.',
      'Modelar y consultar bases de datos relacionales.',
      'Aplicar autenticación, autorización y validaciones.',
    ],
    demandTag: 'Alta demanda laboral',
  },
  {
    id: 'fullstack-dev-jr',
    name: 'Full Stack Developer Jr',
    area: 'Desarrollo',
    icon: 'layers',
    description: 'Participa tanto en la interfaz como en la lógica de negocio de productos digitales.',
    responsibilities: [
      'Construir features de punta a punta.',
      'Integrar frontend y backend de forma coordinada.',
      'Colaborar con todo el equipo de producto.',
    ],
    demandTag: 'Crecimiento acelerado',
  },
  {
    id: 'qa-automation-jr',
    name: 'QA Automation Jr',
    area: 'Calidad',
    icon: 'check',
    description: 'Automatiza y ejecuta pruebas end-to-end para asegurar la calidad del producto.',
    responsibilities: [
      'Diseñar casos de prueba y criterios de aceptación.',
      'Automatizar pruebas funcionales.',
      'Reportar y dar seguimiento a incidencias.',
    ],
    demandTag: 'Testing ágil',
  },
  {
    id: 'data-analyst-jr',
    name: 'Data Analyst Jr',
    area: 'Datos',
    icon: 'chart',
    description: 'Modela datos, construye consultas y comunica hallazgos para apoyar decisiones.',
    responsibilities: [
      'Escribir consultas SQL complejas.',
      'Construir reportes y visualizaciones.',
      'Comunicar insights a áreas no técnicas.',
    ],
    demandTag: 'Crecimiento acelerado',
  },
  {
    id: 'data-engineer-jr',
    name: 'Data Engineer Jr',
    area: 'Datos',
    icon: 'database',
    description: 'Construye y mantiene pipelines de datos confiables para el resto de la organización.',
    responsibilities: [
      'Diseñar modelos de datos normalizados.',
      'Construir procesos de extracción y transformación.',
      'Operar infraestructura de datos en la nube.',
    ],
    demandTag: 'Alta demanda laboral',
  },
  {
    id: 'devops-jr',
    name: 'DevOps / Cloud Jr',
    area: 'Infraestructura',
    icon: 'cloud',
    description: 'Automatiza despliegues y mantiene la infraestructura cloud del producto.',
    responsibilities: [
      'Mantener pipelines de CI/CD.',
      'Administrar infraestructura cloud básica.',
      'Monitorear disponibilidad y seguridad.',
    ],
    demandTag: 'Rol emergente',
  },
  {
    id: 'cybersecurity-jr',
    name: 'Cybersecurity Analyst Jr',
    area: 'Seguridad',
    icon: 'shield',
    description: 'Identifica vulnerabilidades y aplica buenas prácticas de seguridad en la organización.',
    responsibilities: [
      'Relevar vulnerabilidades comunes (OWASP).',
      'Documentar incidentes y buenas prácticas.',
      'Colaborar con desarrollo en mitigaciones.',
    ],
    demandTag: 'Rol emergente',
  },
]

export const roleSkillRequirements: RoleSkillRequirement[] = [
  // Frontend Developer Jr
  { roleId: 'frontend-dev-jr', skillId: 'html-css', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'frontend-dev-jr', skillId: 'js-ts', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'frontend-dev-jr', skillId: 'react', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'frontend-dev-jr', skillId: 'git', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'frontend-dev-jr', skillId: 'teamwork', requiredLevel: 2, importance: 'complementaria' },
  { roleId: 'frontend-dev-jr', skillId: 'nextjs', requiredLevel: 1, importance: 'complementaria' },
  { roleId: 'frontend-dev-jr', skillId: 'testing', requiredLevel: 1, importance: 'complementaria' },

  // Backend Developer Jr
  { roleId: 'backend-dev-jr', skillId: 'js-ts', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'backend-dev-jr', skillId: 'nodejs', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'backend-dev-jr', skillId: 'rest-apis', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'backend-dev-jr', skillId: 'sql', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'backend-dev-jr', skillId: 'git', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'backend-dev-jr', skillId: 'security', requiredLevel: 1, importance: 'complementaria' },
  { roleId: 'backend-dev-jr', skillId: 'testing', requiredLevel: 2, importance: 'complementaria' },

  // Full Stack Developer Jr
  { roleId: 'fullstack-dev-jr', skillId: 'html-css', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'fullstack-dev-jr', skillId: 'js-ts', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'fullstack-dev-jr', skillId: 'react', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'fullstack-dev-jr', skillId: 'nodejs', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'fullstack-dev-jr', skillId: 'rest-apis', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'fullstack-dev-jr', skillId: 'sql', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'fullstack-dev-jr', skillId: 'git', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'fullstack-dev-jr', skillId: 'testing', requiredLevel: 1, importance: 'complementaria' },

  // QA Automation Jr
  { roleId: 'qa-automation-jr', skillId: 'testing', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'qa-automation-jr', skillId: 'js-ts', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'qa-automation-jr', skillId: 'git', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'qa-automation-jr', skillId: 'rest-apis', requiredLevel: 2, importance: 'complementaria' },
  { roleId: 'qa-automation-jr', skillId: 'teamwork', requiredLevel: 2, importance: 'complementaria' },
  { roleId: 'qa-automation-jr', skillId: 'linux', requiredLevel: 1, importance: 'complementaria' },

  // Data Analyst Jr
  { roleId: 'data-analyst-jr', skillId: 'sql', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'data-analyst-jr', skillId: 'python', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'data-analyst-jr', skillId: 'communication', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'data-analyst-jr', skillId: 'cloud', requiredLevel: 1, importance: 'complementaria' },
  { roleId: 'data-analyst-jr', skillId: 'teamwork', requiredLevel: 2, importance: 'complementaria' },

  // Data Engineer Jr
  { roleId: 'data-engineer-jr', skillId: 'sql', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'data-engineer-jr', skillId: 'python', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'data-engineer-jr', skillId: 'cloud', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'data-engineer-jr', skillId: 'linux', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'data-engineer-jr', skillId: 'rest-apis', requiredLevel: 1, importance: 'complementaria' },

  // DevOps / Cloud Jr
  { roleId: 'devops-jr', skillId: 'linux', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'devops-jr', skillId: 'cloud', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'devops-jr', skillId: 'git', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'devops-jr', skillId: 'security', requiredLevel: 2, importance: 'complementaria' },
  { roleId: 'devops-jr', skillId: 'rest-apis', requiredLevel: 1, importance: 'complementaria' },

  // Cybersecurity Analyst Jr
  { roleId: 'cybersecurity-jr', skillId: 'security', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'cybersecurity-jr', skillId: 'linux', requiredLevel: 3, importance: 'esencial' },
  { roleId: 'cybersecurity-jr', skillId: 'communication', requiredLevel: 2, importance: 'esencial' },
  { roleId: 'cybersecurity-jr', skillId: 'git', requiredLevel: 1, importance: 'complementaria' },
  { roleId: 'cybersecurity-jr', skillId: 'rest-apis', requiredLevel: 1, importance: 'complementaria' },
]

export const learningResources: LearningResource[] = [
  { id: 'res-ts-platzi', skillId: 'js-ts', name: 'TypeScript con React', provider: 'Platzi', url: 'https://platzi.com', type: 'curso', isFree: false, priority: 'alta' },
  { id: 'res-ts-udemy', skillId: 'js-ts', name: 'TypeScript desde cero a experto', provider: 'Udemy', url: 'https://udemy.com', type: 'curso', isFree: false, priority: 'alta' },
  { id: 'res-next-yt', skillId: 'nextjs', name: 'Next.js 14 práctico', provider: 'YouTube / Midudev', url: 'https://youtube.com', type: 'video', isFree: true, priority: 'media' },
  { id: 'res-sql-free', skillId: 'sql', name: 'SQL para análisis de datos', provider: 'freeCodeCamp', url: 'https://freecodecamp.org', type: 'curso', isFree: true, priority: 'alta' },
  { id: 'res-react-docs', skillId: 'react', name: 'Documentación oficial de React', provider: 'react.dev', url: 'https://react.dev', type: 'documentacion', isFree: true, priority: 'alta' },
  { id: 'res-testing-platzi', skillId: 'testing', name: 'Testing en JavaScript', provider: 'Platzi', url: 'https://platzi.com', type: 'curso', isFree: false, priority: 'media' },
  { id: 'res-cloud-aws', skillId: 'cloud', name: 'AWS Cloud Practitioner Essentials', provider: 'AWS Skill Builder', url: 'https://skillbuilder.aws', type: 'curso', isFree: true, priority: 'alta' },
  { id: 'res-linux-basics', skillId: 'linux', name: 'Linux Command Line Basics', provider: 'Udemy', url: 'https://udemy.com', type: 'curso', isFree: false, priority: 'media' },
  { id: 'res-security-owasp', skillId: 'security', name: 'OWASP Top 10 explicado', provider: 'OWASP', url: 'https://owasp.org', type: 'articulo', isFree: true, priority: 'alta' },
  { id: 'res-python-free', skillId: 'python', name: 'Python para todos', provider: 'Coursera', url: 'https://coursera.org', type: 'curso', isFree: true, priority: 'media' },
  { id: 'res-node-docs', skillId: 'nodejs', name: 'Node.js Guides', provider: 'nodejs.org', url: 'https://nodejs.org', type: 'documentacion', isFree: true, priority: 'media' },
  { id: 'res-rest-platzi', skillId: 'rest-apis', name: 'Diseño de APIs REST', provider: 'Platzi', url: 'https://platzi.com', type: 'curso', isFree: false, priority: 'media' },
]
