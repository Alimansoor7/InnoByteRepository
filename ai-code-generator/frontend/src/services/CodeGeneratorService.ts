export interface GeneratedCode {
  reactComponents: string;
  springEntities: string;
  restControllers: string;
  apiClient: string;
}

export class CodeGeneratorService {
  async generateApplication(storyKey: string, designFile: File): Promise<GeneratedCode> {
    const formData = new FormData();
    formData.append('storyKey', storyKey);
    formData.append('design', designFile);

    const response = await fetch('/api/generate/fullstack', {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      throw new Error('Generation failed');
    }

    return response.json();
  }
}