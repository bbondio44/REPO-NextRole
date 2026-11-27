import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import { MemoryRouter } from 'react-router-dom'

import { BottomNav } from '@/components/layout/BottomNav'

describe('BottomNav', () => {
  afterEach(() => cleanup())

  it('keeps all five destinations and marks only the current tab', () => {
    render(<MemoryRouter initialEntries={['/']}><BottomNav /></MemoryRouter>)

    const navigation = screen.getByRole('navigation', { name: 'Navegación principal' })
    const destinations = [
      ['Inicio', '/'],
      ['Habilidades', '/habilidades'],
      ['Roles', '/roles'],
      ['Formación', '/formacion'],
      ['Perfil', '/perfil'],
    ] as const

    for (const [label, href] of destinations) {
      expect(screen.getByRole('link', { name: label })).toHaveAttribute('href', href)
      expect(navigation.querySelector(`a[href="${href}"] svg`)).toHaveAttribute('aria-hidden', 'true')
    }

    expect(screen.getByRole('link', { name: 'Inicio' })).toHaveAttribute('aria-current', 'page')
    expect(screen.getByRole('link', { name: 'Habilidades' })).not.toHaveAttribute('aria-current', 'page')
    expect(screen.getByRole('link', { name: 'Inicio' }).querySelector('span')).toHaveClass('h-9', 'w-12', 'bg-primary-600')
    expect(screen.getByRole('link', { name: 'Habilidades' }).querySelector('span')).toHaveClass('h-9', 'w-12', 'bg-transparent', 'text-slate-500')
  })

  it('moves the active icon treatment when the selected route changes', () => {
    render(<MemoryRouter initialEntries={['/']}><BottomNav /></MemoryRouter>)

    fireEvent.click(screen.getByRole('link', { name: 'Roles' }))

    expect(screen.getByRole('link', { name: 'Roles' })).toHaveAttribute('aria-current', 'page')
    expect(screen.getByRole('link', { name: 'Roles' }).querySelector('span')).toHaveClass('bg-primary-600', 'text-white')
    expect(screen.getByRole('link', { name: 'Inicio' }).querySelector('span')).toHaveClass('bg-transparent', 'text-slate-500')
  })
})
